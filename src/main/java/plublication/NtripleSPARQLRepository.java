/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package plublication;

import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author joe
 */
public class NtripleSPARQLRepository extends SPARQLRepository {
    public NtripleSPARQLRepository(String endpointUrl) {
        super(endpointUrl);
       
      //   this.createHTTPClient().getHttpClient()..setPreferredRDFFormat(RDFFormat.NTRIPLES);
    }
}
