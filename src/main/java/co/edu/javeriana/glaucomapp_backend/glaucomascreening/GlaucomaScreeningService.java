package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.javeriana.glaucomapp_backend.common.S3.S3Service;

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
   
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(buf, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, String.class);
            System.out.println("Response: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                int width = ImageIO.read(file.getInputStream()).getWidth();
                int height = ImageIO.read(file.getInputStream()).getHeight();
                return processResponseDataServer(response, width, height);
                //return response.getBody();
            } else if (response.getStatusCode().is4xxClientError()) {
                throw new RuntimeException("Client error from external API: " + response.getStatusCode());
            } else if (response.getStatusCode().is5xxServerError()) {
                throw new RuntimeException("Server error from external API: " + response.getStatusCode());
            }
    
            return null;  
        } catch (HttpServerErrorException e) {
            throw new RuntimeException("Error from external service: " + e.getResponseBodyAsString(), e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while processing the image", e);
        } catch (RestClientException e) {
            throw new RuntimeException("Unexpected error while processing the image", e);
        }
    }
    

    public String generateUniqueImageId() {
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString();
        return "image_" + timestamp + "_" + uuid + ".png";
    }

    


    public ImageProcessingResultDTO processResponseDataServer(ResponseEntity<String> response, int width, int height) {
        ImageProcessingResultDTO processresult = new ImageProcessingResultDTO();
    
        // Configurar el ObjectMapper con las restricciones de longitud
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxStringLength(100_000_000) 
                .build();
    
        JsonFactory jsonFactory = JsonFactory.builder()
                .streamReadConstraints(constraints)
                .build();
    
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
    
        ServerResultDTO result = new ServerResultDTO();
    
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
    
            JsonNode bitmap = jsonNode.path("image").path("bitmap");
            JsonNode coordinates = jsonNode.path("coordinates");
            System.out.println("Coordinates: " + coordinates);
            JsonNode distances = jsonNode.path("distances");
            System.out.println("Distances: " + distances);
            JsonNode perimeters = jsonNode.path("perimeters");
            System.out.println("Perimeters: " + perimeters);
            JsonNode areas = jsonNode.path("areas");
            System.out.println("Areas: " + areas);

            String base64Image = bitmap.asText();
            Base64.getDecoder().decode(base64Image);

            BufferedImage image = postprocessImage(Base64.getDecoder().decode(bitmap.asText()), width, height);
            String bitmapString = bitmap.textValue();

            result.setBitmap(bitmapString);
            
            //System.out.println("Bitmap: ");
            List<Double> coordinatesList = new ArrayList<>();
            coordinates.forEach(coordinate -> coordinatesList.add(coordinate.asDouble()));
            result.setCoordinates(coordinatesList);

            List<Double> pPoints = new ArrayList<>();
            List<Double> qPoints = new ArrayList<>();
            List<Double> rPoints = new ArrayList<>();

            pPoints.add(coordinatesList.get(0));
            pPoints.add(coordinatesList.get(1));

            qPoints.add(coordinatesList.get(2));
            qPoints.add(coordinatesList.get(3));

            rPoints.add(coordinatesList.get(4));
            rPoints.add(coordinatesList.get(5));

            //drawPointsOnImage(image, pPoints, Color.GREEN);
            //drawPointsOnImage(image, qPoints, Color.RED);
            //drawPointsOnImage(image, rPoints, Color.BLUE);

            List<Double> distancesList = new ArrayList<>();
            distances.forEach(distance -> distancesList.add(distance.asDouble()));
            result.setDistances(distancesList);

            List<Double> perimetersList = new ArrayList<>();
            perimeters.forEach(perimeter -> perimetersList.add(perimeter.asDouble()));
            result.setPerimeters(perimetersList);

            List<Double> areasList = new ArrayList<>();
            areas.forEach(area -> areasList.add(area.asDouble()));
            result.setAreas(areasList);

            String fileName = generateUniqueImageId();

            //File outputfile = new File(fileName);
            //ImageIO.write(image, "png", outputfile);
            //System.out.println("Output file: " + outputfile);
            s3Service.uploadImage(image, fileName);
            String url = s3Service.generatePresignedUrl(fileName);
            System.out.println("URL: " + url);
            processresult.setImageUrl(s3Service.generatePresignedUrl(fileName));
            //processresult.setImageUrl(uploadImageToCloud(outputfile));
            processresult.setDistanceRatio((new BigDecimal(result.getDistances().get(1) / result.getDistances().get(0)).setScale(3, RoundingMode.HALF_UP)).doubleValue());
            processresult.setPerimeterRatio((new BigDecimal(result.getPerimeters().get(1) / result.getPerimeters().get(0)).setScale(3, RoundingMode.HALF_UP)).doubleValue());
            processresult.setAreaRatio((new BigDecimal(result.getAreas().get(1) / result.getAreas().get(0)).setScale(3, RoundingMode.HALF_UP)).doubleValue());
            
            processresult.setImageId(fileName);
            processresult.setNeuroretinalRimPerimeter(result.getPerimeters().get(0));
            processresult.setNeuroretinalRimArea(result.getAreas().get(0));
            processresult.setExcavationPerimeter(result.getPerimeters().get(1));
            processresult.setExcavationArea(result.getAreas().get(1));
            processresult.setDdlStage(calculateDDLStage(processresult.getDistanceRatio()));
            processresult.setState(calculateState(processresult.getDdlStage()));

            return processresult;
        } catch (IOException e) {
            e.printStackTrace();
        }
            return processresult;

    }


    public int calculateDDLStage(Double distanceRatio) {
        if(distanceRatio >= 0.4) {
           return 1;
        } else if(distanceRatio >= 0.3) {
            return 2;
        } else if(distanceRatio >= 0.2) {
            return 3;
        } else if(distanceRatio >= 0.1) {
            return 4;
        } else if(distanceRatio > 0) {
            return 5;
        } else {
            return 6;
        }
    }

    public String calculateState(int ddlsStage){
        return switch (ddlsStage) {
            case 1,2,3,4 -> GlaucomaStatus.AT_RISK.getDescription();
            case 5,6,7 -> GlaucomaStatus.GLAUCOMA_DAMAGE.getDescription();
            case 8,9,10 -> GlaucomaStatus.GLAUCOMA_DISABILITY.getDescription();
            default -> "Unknown";
        };
    }



    public void processResponseData(String jsonResponse, MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            JsonNode nerveCoordinates = rootNode.path("nerve");
            JsonNode excavationCoordinates = rootNode.path("excavation");

            BufferedImage image = ImageIO.read(file.getInputStream());
            drawCoordinatesOnImage(image, nerveCoordinates, Color.GREEN);
            drawCoordinatesOnImage(image, excavationCoordinates, Color.BLUE);

             //File outputFile = new File("imagen_modificada.png");
             //ImageIO.write(image, "png", outputFile);


        } catch (JsonProcessingException ex) {
        } catch (IOException ex) {
        }
    }



    private void drawPointsOnImage(BufferedImage image, List<Double> points, Color color) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(color);
        g.setStroke(new BasicStroke(3));
    
        for (int i = 0; i < points.size(); i += 2) {
            if (i + 1 < points.size()) {
                int x = points.get(i).intValue();
                int y = points.get(i + 1).intValue();
                g.fillOval(x - 3, y - 3, 6, 6);
            }
        }
    
        g.dispose();
    }
    


    private void drawCoordinatesOnImage(BufferedImage image, JsonNode coordinates, Color color) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(color);
        g.setStroke(new BasicStroke(3));
    
        int[] xPoints = new int[coordinates.size()/2];
        int[] yPoints = new int[coordinates.size()/2];

        for (int i = 0; i < coordinates.size(); i += 2) {
            if (i + 1 < coordinates.size()) {
                xPoints[i / 2] = (int) coordinates.get(i).asDouble();
                yPoints[i / 2] = (int) coordinates.get(i + 1).asDouble();
            }
        }
        g.drawPolygon(xPoints, yPoints, xPoints.length);        
        g.dispose();
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

        if( pix_size == 1){
            hdr[0] |= (byte) 16;
        }
        else if( pix_size == 2){
            hdr[0] |= (byte) 48;
        }
        else if( pix_size == 4){
            hdr[0] |= (byte) 80;
        } // 
        hdr[0] |= (byte) 4;

        if( channels == 3 )
        {
            hdr[0] |= (byte) 2;
        }
        else if( channels == 4 )
        {
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

    String uploadImageToCloud(File file) {
        return "https://miranza.es/wp-content/uploads/2020/09/Glaucoma-1.jpg";
    }


    public ImageProcessingResultDTO generateResult() {
        ImageProcessingResultDTO result = new ImageProcessingResultDTO();
        result.setImageUrl("http://example.com/processed-image.jpg");
        result.setDistanceRatio(85.0);
        result.setPerimeterRatio(2.0);
        result.setAreaRatio(3.0);
        return result;
    }




}
