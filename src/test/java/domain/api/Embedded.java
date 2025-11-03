package domain.api;

import lombok.Data;

import java.util.List;

@Data
public class Embedded {

    private List<Item> items;
}
