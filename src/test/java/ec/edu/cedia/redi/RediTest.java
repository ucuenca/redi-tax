/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi;

import corticalClasification.AreaUnesco;
import java.util.Arrays;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class RediTest {

    private final Logger log = LoggerFactory.getLogger(RediTest.class);
    private final ValueFactory vf = ValueFactoryImpl.getInstance();
    private static Redi redi;
    private static RediRepository repository;

    public RediTest() {
    }

    @BeforeClass
    public static void setUpClass() throws RepositoryException {
        repository = RediRepository.getInstance();
        redi = new Redi(repository);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        repository.close();
    }

    /**
     * Test of getAuthors method, of class Redi.
     */
    @Test
    public void testGetAuthors() throws Exception {
        assertTrue(!redi.getAuthors().isEmpty());
    }

    /**
     * Test of store method, of class Redi.
     */
    @Test
    public void testStore() throws Exception {
        // TODO: Test over a mock repository
        AreaUnesco area = new AreaUnesco("Computer Science", vf.createURI("http://skos.um.es/unesco6/1203"), 1.1);
        List<AreaUnesco> areas = Arrays.asList(area);
        URI author = vf.createURI("http://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_SAQUICELA_GALARZA_____VICTOR_HUGO_");
//        redi.store(author, areas);
    }

}
