package exportador.servicios;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exportador.modelos.DcOperaciones;
import exportador.repositorios.OperacionesRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OperacionesService implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7358707467450204334L;
	
	private Map<String, String> paramsErrorMap;
	
	@Autowired
	private OperacionesRepository operacionesRepository;
	

	public List<DcOperaciones> obtenerOperaciones() {
		
		return operacionesRepository.findAll();
	}
}