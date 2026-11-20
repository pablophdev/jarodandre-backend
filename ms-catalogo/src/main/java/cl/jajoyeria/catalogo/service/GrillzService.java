package cl.jajoyeria.catalogo.service;
import cl.jajoyeria.catalogo.dto.*;
import cl.jajoyeria.catalogo.entity.*;
import cl.jajoyeria.catalogo.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class GrillzService {
    private final OpcionGrillzRepository opciones;
    private final DisenoGrillzRepository disenos;
    private final ObjectMapper json;
    public List<OpcionGrillzResponse> opciones(){return opciones.findAll().stream().map(o -> new OpcionGrillzResponse(o.getCodigo(),o.getTipo(),o.getNombre(),o.getColor(),o.getPrecioClp())).toList();}
    public long estimar(GrillzRequest r) {
        if(!"arcada-v1".equals(r.modelo())) throw new ResponseStatusException(BAD_REQUEST,"El modelo de grillz no es válido.");
        var ids=new HashSet<String>(); long total=0;
        for(var d:r.dientes()) {
            if(!d.id().matches("[SI][1-8]") || !ids.add(d.id())) throw new ResponseStatusException(BAD_REQUEST,"Selecciona dientes válidos, sin repetirlos.");
            total+=opcion(d.material(),"MATERIAL").getPrecioClp()+opcion(d.estilo(),"ESTILO").getPrecioClp();
        }
        return total;
    }
    private OpcionGrillz opcion(String codigo,String tipo) {
        var o=opciones.findById(codigo).orElseThrow(()->new ResponseStatusException(BAD_REQUEST,"Selecciona un acabado disponible."));
        if(!tipo.equals(o.getTipo())) throw new ResponseStatusException(BAD_REQUEST,"La opción no corresponde al acabado elegido.");
        return o;
    }
    @Transactional public GrillzResponse guardar(GrillzRequest r, CompraVerificadaClient.Perfil perfil) {
        long precio=estimar(r);
        try {return respuesta(disenos.save(new DisenoGrillz(perfil.id(),perfil.nombre(),r.nombre().strip(),json.writeValueAsString(r),precio)));}
        catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw new IllegalStateException(ex);}
    }
    public Page<GrillzResponse> mios(UUID usuario,int pagina){return disenos.findByUsuarioIdOrderByCreadoEnDesc(usuario,PageRequest.of(pagina,20)).map(this::respuesta);}
    public Page<GrillzResponse> solicitudes(int pagina){return disenos.findByEstadoOrderByCreadoEnDesc("SOLICITADO",PageRequest.of(pagina,20)).map(this::respuesta);}
    @Transactional public GrillzResponse solicitar(UUID id,UUID usuario) {
        var d=disenos.findById(id).filter(x->x.getUsuarioId().equals(usuario)).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Diseño no encontrado."));
        if(!"SOLICITADO".equals(d.getEstado())) d.solicitar();
        return respuesta(d);
    }
    private GrillzResponse respuesta(DisenoGrillz d) {
        try{return new GrillzResponse(d.getId(),d.getUsuarioId(),d.getAutor(),d.getNombre(),json.readValue(d.getConfiguracion(),GrillzRequest.class),d.getEstimadoClp(),d.getEstado(),d.getCreadoEn());}
        catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw new IllegalStateException(ex);}
    }
}
