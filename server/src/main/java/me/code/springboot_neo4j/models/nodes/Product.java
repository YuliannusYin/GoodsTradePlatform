package me.code.springboot_neo4j.models.nodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Node("Product")
public class Product {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;
    private String name;
    private String description;
    private List<String> imageUrls;
    private double price;
    private int quantity;
    private String category;
    private Condition condition;
    private String source;

    @Relationship(type = "SOLD_BY", direction = Relationship.Direction.INCOMING)
    private User seller;

    public Product(String name, String description, List<String> imageUrls, double price, int quantity, String category, Condition condition, String source) {
        this.name = name;
        this.description = description;
        this.imageUrls = imageUrls;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.condition = condition;
        this.source = source;
    }

    public enum Condition {
        NEW, LIKE_NEW, GOOD, FAIR
    }

    public enum Category {
        ANIME_FIGURE, POSTER, KEYCHAIN, BADGE, PILLOW, STAND, CLOTHING, ALBUM, ACCESSORY, OTHER
    }
}
