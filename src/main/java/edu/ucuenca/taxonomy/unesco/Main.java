/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.taxonomy.unesco;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class Main {

    public static void main(String[] args) {
        ValueFactory vf = ValueFactoryImpl.getInstance();
        URI two_digit = vf.createURI("http://skos.um.es/unesco6/12");
        URI four_digit = vf.createURI("http://skos.um.es/unesco6/1203");
        URI six_digit = vf.createURI("http://skos.um.es/unesco6/120304");
        try (UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance()) {
            UnescoNomeclature un = new UnescoNomeclature(conn);

            System.out.println();
            for (URI field : un.twoDigitResources()) {
                System.out.print(field);
                System.out.println("->" + un.getLabel(field, "en").getLabel());
                for (URI discipline : un.narrow(field)) {
                    System.out.print("\t" + discipline);
                    System.out.println("->" + un.getLabel(discipline, "en").getLabel());
                    for (URI subdiscipline : un.narrow(discipline)) {
                        System.out.print("\t\t" + subdiscipline);
                        System.out.println("->" + un.getLabel(subdiscipline, "en").getLabel());
                    }
                }
            }
        } catch (RepositoryException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
