package exportador.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import exportador.modelos.Entidad;
import exportador.servicios.EntidadService;

@RestController
@RequestMapping("/v1/api/")
public class AdministracionWebController {

	private static final Logger logger = LogManager.getLogger(AdministracionWebController.class);
	
	 @Autowired
	 private EntidadService entityService;
	
    private List<Entidad> entities;

    @PostMapping("upload")
    public List<Entidad> uploadFile(@RequestParam("file") MultipartFile file) {
        entities = entityService.loadDataFromFile(file);
        return entities;
    }

    @GetMapping
    public List<Entidad> getEntities() {
        return entities;
    }
}
