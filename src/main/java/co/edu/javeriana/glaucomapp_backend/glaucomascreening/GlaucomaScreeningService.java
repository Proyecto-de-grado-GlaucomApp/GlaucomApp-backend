package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.javeriana.glaucomapp_backend.s3.exposed.S3Service;

@Service
public class GlaucomaScreeningService {

    @Value("${PYTHON_API_URL}")
    private String pythonApiUrl;

    private final S3Service s3Service;

    public GlaucomaScreeningService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public ImageProcessingResultDTO sendImageToApi(MultipartFile file) {
        try {
            byte[] buf = preprocessImage(file);
            System.out.println("En el servicio");

            ResponseEntity<String> response = sendImageToExternalApi(buf);

            return handleApiResponse(file, response);
        } catch (Exception e) {
            handleImageProcessingException(e);
        }
        return null; // Considerar lanzar una excepción o manejar un resultado nulo
    }

    private ResponseEntity<String> sendImageToExternalApi(byte[] buf) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(buf, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, String.class);
        System.out.println("Response: " + response.getStatusCode());

        return response;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return headers;
    }

    private void handleImageProcessingException(Exception e) {
        if (e instanceof HttpServerErrorException) {
            throw new RuntimeException(
                    "Error from external service: " + ((HttpServerErrorException) e).getResponseBodyAsString(), e);
        } else if (e instanceof IOException) {
            throw new RuntimeException("I/O error while processing the image", e);
        } else if (e instanceof RestClientException) {
            throw new RuntimeException("Unexpected error while processing the image", e);
        }
    }

    private ImageProcessingResultDTO handleApiResponse(MultipartFile file, ResponseEntity<String> response)
            throws IOException {
        if (response.getStatusCode().is2xxSuccessful()) {
            int width = ImageIO.read(file.getInputStream()).getWidth();
            int height = ImageIO.read(file.getInputStream()).getHeight();
            return processApiResponseData(response, width, height);
        } else {
            handleApiError(response);
        }
        return null;
    }

    private void handleApiError(ResponseEntity<String> response) {
        if (response.getStatusCode().is4xxClientError()) {
            throw new RuntimeException("Client error from external API: " + response.getStatusCode());
        } else if (response.getStatusCode().is5xxServerError()) {
            throw new RuntimeException("Server error from external API: " + response.getStatusCode());
        }
    }

    public String generateUniqueImageId() {
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString();
        return "image_" + timestamp + "_" + uuid + ".png";
    }

    public ImageProcessingResultDTO processApiResponseData(ResponseEntity<String> response, int width, int height) {
        ImageProcessingResultDTO processresult = new ImageProcessingResultDTO();
        ObjectMapper objectMapper = configureObjectMapper();

        try {
            ServerResultDTO result = parseResponse(response, objectMapper);
            BufferedImage image = postprocessImageData(result, width, height);

            uploadImageToS3(image, processresult, result);

            calculateRatiosAndSetResult(processresult, result);

            return processresult;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processresult;
    }

    private void calculateRatiosAndSetResult(ImageProcessingResultDTO processresult, ServerResultDTO result) {
        processresult.setDistanceRatio(calculateRatio(result.getDistances()));
        processresult.setPerimeterRatio(calculateRatio(result.getPerimeters()));
        processresult.setAreaRatio(calculateRatio(result.getAreas()));
        processresult.setDdlStage(calculateDDLStage(processresult.getDistanceRatio()));
        processresult.setState(calculateState(processresult.getDdlStage()));
    }

    private double calculateRatio(List<Double> values) {
        return new BigDecimal(values.get(1) / values.get(0)).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private void uploadImageToS3(BufferedImage image, ImageProcessingResultDTO processresult, ServerResultDTO result) {
        String fileName = generateUniqueImageId();
        s3Service.uploadImage(image, fileName);
        String url = s3Service.generatePresignedUrl(fileName);
        processresult.setImageUrl(url);
        processresult.setImageId(fileName);
        processresult.setNeuroretinalRimPerimeter(result.getPerimeters().get(0));
        processresult.setNeuroretinalRimArea(result.getAreas().get(0));
        processresult.setExcavationPerimeter(result.getPerimeters().get(1));
        processresult.setExcavationArea(result.getAreas().get(1));
    }

    private BufferedImage postprocessImageData(ServerResultDTO result, int width, int height) throws IOException {
        String base64Image = result.getBitmap();
        BufferedImage image = postprocessImage(Base64.getDecoder().decode(base64Image), width, height);
        return image;
    }

    private ObjectMapper configureObjectMapper() {
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxStringLength(100_000_000)
                .build();

        JsonFactory jsonFactory = JsonFactory.builder()
                .streamReadConstraints(constraints)
                .build();

        return new ObjectMapper(jsonFactory);
    }

    private ServerResultDTO parseResponse(ResponseEntity<String> response, ObjectMapper objectMapper)
            throws IOException {
        JsonNode jsonNode = objectMapper.readTree(response.getBody());

        ServerResultDTO result = new ServerResultDTO();
        JsonNode bitmap = jsonNode.path("image").path("bitmap");
        JsonNode coordinates = jsonNode.path("coordinates");
        JsonNode distances = jsonNode.path("distances");
        JsonNode perimeters = jsonNode.path("perimeters");
        JsonNode areas = jsonNode.path("areas");

        System.out.println("Coordinates: " + coordinates);
        System.out.println("Distances: " + distances);
        System.out.println("Perimeters: " + perimeters);
        System.out.println("Areas: " + areas);

        String base64Image = bitmap.asText();
        Base64.getDecoder().decode(base64Image);

        result.setBitmap(bitmap.textValue());
        result.setCoordinates(parseCoordinates(coordinates));
        result.setDistances(parseListFromJsonNode(distances));
        result.setPerimeters(parseListFromJsonNode(perimeters));
        result.setAreas(parseListFromJsonNode(areas));

        return result;
    }

    private List<Double> parseCoordinates(JsonNode coordinates) {
        List<Double> coordinatesList = new ArrayList<>();
        coordinates.forEach(coordinate -> coordinatesList.add(coordinate.asDouble()));
        return coordinatesList;
    }

    private List<Double> parseListFromJsonNode(JsonNode jsonNode) {
        List<Double> list = new ArrayList<>();
        jsonNode.forEach(item -> list.add(item.asDouble()));
        return list;
    }

    public int calculateDDLStage(Double distanceRatio) {
        if (distanceRatio >= 0.4) {
            return 1;
        } else if (distanceRatio >= 0.3) {
            return 2;
        } else if (distanceRatio >= 0.2) {
            return 3;
        } else if (distanceRatio >= 0.1) {
            return 4;
        } else if (distanceRatio > 0) {
            return 5;
        } else {
            return 6;
        }
    }

    public int calculateState(int ddlsStage) {
        return switch (ddlsStage) {
            case 1, 2, 3, 4 -> GlaucomaStatus.AT_RISK.getCode();
            case 5, 6, 7 -> GlaucomaStatus.GLAUCOMA_DAMAGE.getCode();
            case 8, 9, 10 -> GlaucomaStatus.GLAUCOMA_DISABILITY.getCode();
            default -> 0;
        };
    }

    public byte[] preprocessImage(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        int height = image.getHeight();
        int width = image.getWidth();
        int channels = image.getColorModel().getNumComponents();
        long size = height * width * channels;
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        long pix_size = pixels.length / size;
        System.out.println("Pix_Size:" + pix_size);
        System.out.println("pixels.length:" + pixels.length);
        System.out.println("size:" + size);
        System.out.println("channels:" + channels);
        System.out.println("height:" + height);
        System.out.println("width:" + width);

        byte[] hdr;
        hdr = new byte[1];
        hdr[0] = (byte) 0x00;

        if (pix_size == 1) {
            hdr[0] |= (byte) 16;
        } else if (pix_size == 2) {
            hdr[0] |= (byte) 48;
        } else if (pix_size == 4) {
            hdr[0] |= (byte) 80;
        } //
        hdr[0] |= (byte) 4;

        if (channels == 3) {
            hdr[0] |= (byte) 2;
        } else if (channels == 4) {
            hdr[0] |= (byte) 3;
        }

        System.out.println("hdr:" + hdr[0]);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN).put(hdr[0]).array());
        buf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(height).array());
        buf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(width).array());

        float[] spac = new float[2];
        float[] orig = new float[2];
        for (int i = 0; i < 2; i++) {
            spac[i] = 1.0f;
            orig[i] = 0.0f;
        }
        for (float s : spac) {
            buf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(s).array());
        }
        for (float o : orig) {
            buf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(o).array());
        }

        buf.write(pixels);
        System.out.println("buf:" + buf.size());

        return buf.toByteArray();

    }

    public BufferedImage postprocessImage(byte[] data, int width, int height) throws IOException {

        data = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).array();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int b = data[index++] & 0xFF;
                int g = data[index++] & 0xFF;
                int r = data[index++] & 0xFF;
                int rgb = (r << 16) | (g << 8) | (b);
                image.setRGB(x, y, rgb);
            }
        }

        return image;
    }

}
