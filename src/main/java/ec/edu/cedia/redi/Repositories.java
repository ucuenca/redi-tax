/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ec.edu.cedia.redi;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author joe
 */
public interface Repositories {
    public String getContext ();
    public String getClusterGraph ();
    public String getAuthorGraph ();
    public RepositoryConnection getConnection() throws RepositoryException;
}
