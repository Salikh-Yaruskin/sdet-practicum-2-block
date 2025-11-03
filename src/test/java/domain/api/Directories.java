package domain.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Directories {

    @JsonProperty("_embedded")
    private Embedded embedded;
}
