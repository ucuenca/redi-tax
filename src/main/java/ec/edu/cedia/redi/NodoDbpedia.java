/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ec.edu.cedia.redi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joe
 */
public class NodoDbpedia {
    String nameEs;
    String nameEn;
    String origin;
    String uri ;
    List <NodoDbpedia> broader;
    List <NodoDbpedia> near;
    List <NodoDbpedia> academic;

    public List<NodoDbpedia> getAcademic() {
        return academic;
    }

    public void setAcademic(List<NodoDbpedia> academic) {
        this.academic = academic;
    }
    
    public List<NodoDbpedia> getBroader() {
        return broader;
    }

    public void setBroader(List<NodoDbpedia> broader) {
        this.broader = broader;
    }

    public List<NodoDbpedia> getNarrower() {
        return near;
    }

    public void setNarrower(List<NodoDbpedia> narrower) {
        this.near = narrower;
    }
    

    public String getNameEs() {
        return nameEs;
    }

    public void setNameEs(String nameEs) {
        this.nameEs = nameEs;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public NodoDbpedia(String uri) {
           this.uri = uri;
           academic = new ArrayList<>();
    }
    
    
}
