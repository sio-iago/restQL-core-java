package restql.core.query;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class QueryInterpolatorTest extends TestCase {

    @Test
    public void testSimpleQueryParse() {
        String query = "from cards as c";

        assertEquals(query, QueryInterpolator.interpolate(query, "foo", "bar"));
    }

    @Test
    public void testSimpleQueryWildcardInt() {
        String query = "from cards as c with id = ?";
        String expectedResult = "from cards as c with id = 1";

        assertEquals(expectedResult, QueryInterpolator.interpolate(query, 1));
    }

    @Test
    public void testSimpleQueryWildcardDouble() {
        String query = "from products with price = ?";
        String expectedResult = "from products with price = 19.99";

        assertEquals(expectedResult, QueryInterpolator.interpolate(query, 19.99F));
    }

    @Test
    public void testSimpleQueryWildcardString() {
        String query = "from products with name = ?";
        String expectedResult = "from products with name = \"iPhone\"";

        assertEquals(expectedResult, QueryInterpolator.interpolate(query, "iPhone"));
    }

    @Test
    public void testSimpleQueryWildcardArray() {
        String query = "from products with name = ?";
        String expectedResult = "from products with name = [\"iPhone\",\"Galaxy\"]";

        List<String> names = new ArrayList<>();
        names.add("iPhone");
        names.add("Galaxy");

        assertEquals(expectedResult, QueryInterpolator.interpolate(query, names));
    }

    @Test
    public void testQueryWildcardMultipleTypes() {
        String query = "from products with name = ?, price = ?, version = ?, stock = ?";
        String expectedResult = "from products with name = [\"iPhone\",\"Galaxy\"], price = 19.99,"
                + " version = 7, stock = true";

        List<String> names = new ArrayList<>();
        names.add("iPhone");
        names.add("Galaxy");

        assertEquals(expectedResult, QueryInterpolator.interpolate(query,
                names,
                19.99,
                7,
                true));
    }

}
