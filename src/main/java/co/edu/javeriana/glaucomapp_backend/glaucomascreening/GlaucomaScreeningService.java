package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GlaucomaScreeningService {

    @Value("${PYTHON_API_URL}") 
    private String pythonApiUrl;

    public String sendImageToApi(MultipartFile file) {
        try {
            byte[] buf = preprocessImage(file);

            //BufferedImage bufImg = postprocessImage(buf);
            // Define la ruta donde deseas guardar la imagen
            //String outputPath = "imagePost.png";
            //File outputfile = new File(outputPath);
            //ImageIO.write(bufImg, "png", outputfile);

   
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(buf, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                //processResponseData(response.getBody(), file);
                processResponseDataServer(response);
                return response.getBody();
            } else if (response.getStatusCode().is4xxClientError()) {
                throw new RuntimeException("Client error from external API: " + response.getStatusCode());
            } else if (response.getStatusCode().is5xxServerError()) {
                throw new RuntimeException("Server error from external API: " + response.getStatusCode());
            }
    
            return null;  // Control should not reach here, but for safety
        } catch (HttpServerErrorException e) {
            throw new RuntimeException("Error from external service: " + e.getResponseBodyAsString(), e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while processing the image", e);
        } catch (RestClientException e) {
            throw new RuntimeException("Unexpected error while processing the image", e);
        }
    }
    


    public void processResponseDataServer(ResponseEntity<String> response){
       
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = response.getBody();

        ServerResultDTO result = new ServerResultDTO();
        System.out.println("Dentro de processResponseDataServer");

        

            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                System.out.println("Se ha extraido el jsonNode");
                //System.out.println("JsonNode: " + jsonNode);

                //Escribir en un txt jsonnode
                BufferedWriter writerQ = new BufferedWriter(new FileWriter("jsonNode.txt"));
                writerQ.write(jsonNode + "\n");
                
                JsonNode bitmap = jsonNode.path("image").path("bitmap");
                JsonNode coordinates = jsonNode.path("coordinates");
                JsonNode distances = jsonNode.path("distances");
                JsonNode perimeters = jsonNode.path("perimeters");
                JsonNode areas = jsonNode.path("areas");

                System.out.println("Pruebas...");

                System.out.println("Bitmap: " + jsonNode.toString().substring(0, 50));
                System.out.println("Bitmap: " + jsonNode.path("image").path("bitmap").toString().substring(0, 50));



                            // Ejemplo de cadena Base64 que contiene la imagen en formato texto
            String base64Image = bitmap.asText();  // reemplaza con tu valor de Base64



                //System.out.println("Bitmap: " + Arrays.toString(bitmap.asText().getBytes()));


                            // Decodifica el base64 a un arreglo de bytes
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            System.out.println("ImageBytes: " + imageBytes);

           
            // Crea un InputStream desde los bytes
            //ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            
            // Lee la imagen desde el InputStream
            BufferedImage image = postprocessImage(base64Image.getBytes());
            
            // Guarda la imagen como PNG o JPEG
            File outputfile = new File("output.png"); // Cambia la extensión si deseas guardar en otro formato, por ejemplo, "output.jpg"
            ImageIO.write(image, "png", outputfile);  // Cambia "png" por "jpeg" si deseas guardar en JPEG
                System.out.println("Bitmap: " + bitmap.toString());

                String bitmapString = bitmap.textValue();
                System.out.println("Bitmap: " + bitmap.toString());
                result.setBitmap(bitmapString);
            
            List<Double> coordinatesList = new ArrayList<>();
            coordinates.forEach(coordinate -> coordinatesList.add(coordinate.asDouble()));
            result.setCoordinates(coordinatesList);

            List<Double> distancesList = new ArrayList<>();
            distances.forEach(distance -> distancesList.add(distance.asDouble()));
            result.setDistances(distancesList);

            List<Double> perimetersList = new ArrayList<>();
            perimeters.forEach(perimeter -> perimetersList.add(perimeter.asDouble()));
            result.setPerimeters(perimetersList);

            List<Double> areasList = new ArrayList<>();
            areas.forEach(area -> areasList.add(area.asDouble()));
            result.setAreas(areasList);

            System.out.println("Result: " + result.toString());


            System.out.println("Response: " + bitmap);

            //Escribir bitmap en un txt



            //BufferedWriter writerQ = new BufferedWriter(new FileWriter("responsepRE.txt"));
            //writerQ.write(response.getBody() + "\n");
            // Deserializa el JSON en ServerResultDTO
            
            BufferedWriter writer = new BufferedWriter(new FileWriter("response.txt"));

            writer.write(jsonResponse + "\n");

            //ServerResultDTO result = objectMapper.readValue(jsonResponse, ServerResultDTO.class);

        } catch (IOException e) {
            e.printStackTrace();
        }

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

             File outputFile = new File("imagen_modificada.png");
             ImageIO.write(image, "png", outputFile);
        } catch (JsonProcessingException ex) {
        } catch (IOException ex) {
        }
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

        byte[] hdr;
        hdr = new byte[1];
        hdr[0] = (byte) 0x00;

        if( pix_size == 1){
            hdr[0] |= ( 1 << 4 );
        }
        else if( pix_size == 2){
            hdr[0] |= ( 3 << 4 );
        }
        else if( pix_size == 4){
            hdr[0] |= ( 5 << 4 );
        } // 
        hdr[0] |= (byte) 0x20;

        if( channels == 3 )
        {
            hdr[0] |= 2;
        }
        else if( channels == 4 )
        {
            hdr[0] |= 3;
        }
        hdr[0] = (byte) 0x16;
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
        return buf.toByteArray();
        
    }

    public BufferedImage postprocessImage(byte[] data) throws IOException {
        // Crear un ByteBuffer para leer los datos
        //System.out.println("Data:" + data.toString());
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        //System.out.println("Buffer:" + buffer.get());
            
        // Leer la cabecera
        byte hdr = buffer.get();
        System.out.println("hdr:" + hdr);

        // Leer el alto y el ancho
        int height = buffer.getInt();
        int width = buffer.getInt();

        System.out.println("height:" + height);
        System.out.println("width:" + width);

        // Leer los espacios y los orígenes (descartarlos)
    buffer.getFloat(); 
    buffer.getFloat(); 
    buffer.getFloat(); 
    buffer.getFloat();

    // Determinar los canales a partir de la cabecera
    int channels = (hdr & 0x03) == 3 ? 3 : 4; // 2 -> 3 canales, 3 -> 4 canales

    System.out.println("channels:" + channels);
    // Determinar el tamaño del píxel a partir de la cabecera
    int pixSize = 1;
    System.out.println("pixSize:" + pixSize);
    int bytesPerPixel = 1;
    
    if (pixSize == 3) {
        bytesPerPixel = 2;
    } else if (pixSize == 5) {
        bytesPerPixel = 4;
    }

    // Calcular el número total de píxeles
    int numPixels = height * width * 3;
    System.out.println("numPixels:" + numPixels);

    // Extraer los datos de píxeles
    byte[] pixels = new byte[numPixels * bytesPerPixel];
    System.out.println("pixels:" + pixels);
    buffer.get(pixels);

    // Crear un BufferedImage basado en la información recuperada
    BufferedImage image = new BufferedImage(width, height, 
                                            (3 == 4) ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);

    // Cargar los píxeles en el BufferedImage
    image.getRaster().setDataElements(0, 0, width, height, pixels);
 

    
        return image;
    }
    

    
    //Genrate result from analysis
    public ImageProcessingResultDTO generateResult() {
        
        return new ImageProcessingResultDTO(
            "http://example.com/processed-image.jpg",
            "Imagen procesada correctamente.",
            85,
            2
        );
    }




}
