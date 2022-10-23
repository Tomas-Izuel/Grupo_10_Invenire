package com.example.Invenire.controllers;

import com.example.Invenire.entities.CuponDescuento;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("api/descuento")
public class CuponDescuentoController extends BaseControllerImpl<CuponDescuento, CuponDescuentoServiceImpl>{
}
