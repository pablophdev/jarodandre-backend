package cl.jajoyeria.catalogo.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record GrillzRequest(@NotBlank @Size(max=100) String nombre,
        @NotBlank @Size(max=40) String modelo, @NotEmpty @Size(max=16) List<@NotNull @Valid Diente> dientes) {
    public record Diente(@NotBlank @Pattern(regexp="[SI][1-8]") String id, @NotBlank @Size(max=40) String material, @NotBlank @Size(max=40) String estilo) {}
}
