package com.example.Invenire.services;

import com.example.Invenire.entities.dtos.UsuarioEditDTO;
import com.example.Invenire.entities.dtos.UsuarioRegistroDTO;
import com.example.Invenire.entities.entities.Direccion;
import com.example.Invenire.entities.entities.Pais;
import com.example.Invenire.entities.entities.Role;
import com.example.Invenire.entities.entities.Usuario;
import com.example.Invenire.repositories.DireccionRepository;
import com.example.Invenire.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioDetailsServiceImpl implements UsuarioDetailsService {

    private UsuarioRepository usuarioRepositorio;
    private DireccionRepository direccionRepository;
    @Autowired private RoleServiceImpl roleService;

    @Autowired private PaisServiceImpl paisServiceImpl;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public UsuarioDetailsServiceImpl(UsuarioRepository usuarioRepositorio) {
        super();
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public Usuario registerUser(UsuarioRegistroDTO registroDTO) {
        try{
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(registroDTO.getFechaNacimiento());
            Role rolUser = roleService.findByNombre("ROLE_USER");
            Usuario usuario = Usuario.builder()
                    .username(registroDTO.getUsername())
                    .password(passwordEncoder.encode(registroDTO.getPassword()))
                    .email(registroDTO.getEmail())
                    .nombre(registroDTO.getNombre())
                    .apellido(registroDTO.getApellido())
                    .celular(registroDTO.getCelular())
                    .fechaNacimiento(date)
                    .roles(Arrays.asList(rolUser))
                    .build();
            return usuarioRepositorio.save(usuario);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public Usuario editUser(UsuarioEditDTO editDTO, Usuario userNativo){
        try{
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(editDTO.getFechaNacimiento());

            userNativo.setCelular(editDTO.getCelular());
            userNativo.setEmail(editDTO.getEmail());
            userNativo.setFechaNacimiento(date);

            if(userNativo.getDireccion() != null){
                userNativo.getDireccion().setPais(paisServiceImpl.findPaisByNombre(editDTO.getPais()));
                userNativo.getDireccion().setCalle(editDTO.getCalle());
                userNativo.getDireccion().setCiudad(editDTO.getCiudad());
                userNativo.getDireccion().setLocalidad(editDTO.getLocalidad());
                userNativo.getDireccion().setCodPostal(editDTO.getCodPostal());
            }else{
                Direccion direccion = Direccion.builder()
                        .calle(editDTO.getCalle())
                        .ciudad(editDTO.getCiudad())
                        .localidad(editDTO.getLocalidad())
                        .codPostal(editDTO.getCodPostal())
                        .pais(paisServiceImpl.findPaisByNombre(editDTO.getPais()))
                        .build();
                userNativo.setDireccion(direccion);

            }
            return usuarioRepositorio.save(userNativo);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }

    }

    @Override
    public Usuario obtenerUsuarioSesion() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if(auth.getPrincipal() != "anonymousUser") {
            System.out.println(auth.getPrincipal());
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            Usuario usuario = usuarioRepositorio.findByUsername(userDetail.getUsername());
            return usuario;
        }
        return null;

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByUsername(username);
        if(usuario == null) {
            throw new UsernameNotFoundException("Usuario o password inválidos");
        }
        return new User(usuario.getUsername(),usuario.getPassword(), mapearAutoridadesRoles(usuario.getRoles()));
    }

    private Collection<? extends GrantedAuthority> mapearAutoridadesRoles(List<Role> roles){
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getNombre())).collect(Collectors.toList());
    }

}
