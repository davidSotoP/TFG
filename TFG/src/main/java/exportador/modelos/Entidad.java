package exportador.modelos;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Entidad {

	public Map<String, Object> campos = new HashMap<>();
	
	public void setCampos(String key, Object value) {
		campos.put(key, value);
    }

}
