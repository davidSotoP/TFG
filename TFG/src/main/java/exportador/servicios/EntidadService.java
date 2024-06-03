package exportador.servicios;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import exportador.modelos.Entidad;

@Service
public class EntidadService {

    public List<Entidad> loadDataFromFile(MultipartFile file) {
        List<Entidad> entities = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            String[] headers = reader.readLine().split(",");
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Entidad entity = new Entidad();
                for (int i = 0; i < headers.length; i++) {
                    entity.setCampos(headers[i], values[i]);
                }
                entities.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return entities;
    }
}