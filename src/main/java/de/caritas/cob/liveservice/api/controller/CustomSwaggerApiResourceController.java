package de.caritas.cob.liveservice.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.swagger.web.ApiResourceController;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

@Controller
@ApiIgnore
@RequestMapping(value = "${springfox.docuPath}" + "/swagger-resources")
public class CustomSwaggerApiResourceController extends ApiResourceController {

  public CustomSwaggerApiResourceController(SwaggerResourcesProvider swaggerResources) {
    super(swaggerResources);
  }

}
