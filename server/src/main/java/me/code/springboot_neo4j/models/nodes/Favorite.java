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

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Node("Favorite")
public class Favorite {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;
    private LocalDateTime createdAt;

    @Relationship(type = "FAVORITED_BY", direction = Relationship.Direction.OUTGOING)
    private User user;

    @Relationship(type = "FAVORITE_PRODUCT", direction = Relationship.Direction.OUTGOING)
    private Product product;

    public Favorite(User user, Product product) {
        this.user = user;
        this.product = product;
        this.createdAt = LocalDateTime.now();
    }
}
