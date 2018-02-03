package restql.core.examples;

import restql.core.annotation.RestEntity;
import restql.core.manager.RestEntityManager;
import restql.core.query.QueryOptions;
import restql.core.response.QueryResponse;

import java.util.List;

@RestEntity(
        name = "cards",
        resourceMapping = "http://api.magicthegathering.io/v1/cards/:id",
        responseLookupPath = "cards.cards"
)
class Card {
    private Long number;
    private String type;
    private String name;

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

public class SimpleQueryWithAnnotation {

    public static void main(String[] args) {

        QueryOptions opts = new QueryOptions();
        opts.setDebugging(false);
        opts.setGlobalTimeout(10000);
        opts.setTimeout(3000);

        RestEntityManager entityManager = new RestEntityManager(opts);
        List<Card> cards = entityManager.fetch(Card.class);

        for (Card card : cards) {
            System.out.println(String.format("@{%d} %s - %s", card.getNumber(), card.getType(), card.getName()));
        }
    }
}
