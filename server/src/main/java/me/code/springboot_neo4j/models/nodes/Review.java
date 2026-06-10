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
@Node("Review")
public class Review {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    @Relationship(type = "WRITTEN_BY", direction = Relationship.Direction.OUTGOING)
    private User user;

    @Relationship(type = "ABOUT", direction = Relationship.Direction.OUTGOING)
    private Product product;

    public Review(int rating, String comment, User user, Product product) {
        this.rating = rating;
        this.comment = comment;
        this.user = user;
        this.product = product;
        this.createdAt = LocalDateTime.now();
    }
}
