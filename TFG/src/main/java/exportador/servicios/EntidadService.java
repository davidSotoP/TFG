package exportador.servicios;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
    
    public List<Entidad> obtenerDatos(String urlConexion, String username, String password, String nombreTabla) {
    	Connection connection = null;
    	List<Entidad> entidades = new ArrayList<>();
		try {
			connection = DriverManager.getConnection(urlConexion, username, password);
			
			//Se obtiene los datos de la tabla de base de datos
			PreparedStatement selectTable = connection
					.prepareStatement("Select * from "
							+ nombreTabla.toLowerCase() + " LIMIT 500");
			ResultSet resultado = selectTable.executeQuery();
			
			//Se obtiene los datos de la tabla de base de datos
			ResultSetMetaData columnas = resultado.getMetaData();	
			int numColumna = columnas.getColumnCount();
			List<String> listaColumnas = new ArrayList<String>();
			for (int i=1; i <= numColumna; ++i) {
			    String name = columnas.getColumnName(i);
			    listaColumnas.add(name);
			}
			
			while (resultado.next()) {
				Entidad entity = new Entidad();
				for (int i = 1; i <= listaColumnas.size(); i++) {
					String column =listaColumnas.get(i-1);
					Object object = resultado.getObject(i);
					entity.setCampos(column, object);
				}
				entidades.add(entity);
			}
		} catch (SQLException e) {
			return entidades;
		}
		
		return entidades;
    	
    }
}