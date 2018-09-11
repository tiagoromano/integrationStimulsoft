package app.entity;

import java.io.*;
import javax.persistence.*;
import java.util.*;
import javax.xml.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFilter;
import cronapi.rest.security.CronappSecurity;


/**
 * Classe que representa a tabela TESTE
 * @generated
 */
@Entity
@Table(name = "\"TESTE\"")
@XmlRootElement
@CronappSecurity
@JsonFilter("app.entity.Teste")
public class Teste implements Serializable {

  /**
   * UID da classe, necessário na serialização
   * @generated
   */
  private static final long serialVersionUID = 1L;

  /**
   * @generated
   */
  @Id
  @Column(name = "id", nullable = false, insertable=true, updatable=true)
  private java.lang.String id = UUID.randomUUID().toString().toUpperCase();

  /**
  * @generated
  */
  @Temporal(TemporalType.DATE)
  @Column(name = "fDate", nullable = true, unique = false, insertable=true, updatable=true)
  
  private java.util.Date fdate;

  /**
  * @generated
  */
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "fTimeStamp", nullable = true, unique = false, insertable=true, updatable=true)
  
  private java.util.Date ftimeStamp;

  /**
  * @generated
  */
  @Column(name = "fInteger", nullable = true, unique = false, insertable=true, updatable=true)
  
  private java.lang.Integer finteger;

  /**
  * @generated
  */
  @Column(name = "fLong", nullable = true, unique = false, insertable=true, updatable=true)
  
  private java.lang.Long flong;

  /**
  * @generated
  */
  @Column(name = "fDouble", nullable = true, unique = false, insertable=true, updatable=true)
  
  private java.lang.Double fdouble;

  /**
  * @generated
  */
  @Column(name = "fBoolean", nullable = true, unique = false, insertable=true, updatable=true)
  
  private java.lang.Boolean fboolean;

  /**
  * @generated
  */
  @Column(name = "fFloat", nullable = true, unique = false, insertable=true, updatable=true)
  
  private float ffloat;

  /**
   * Construtor
   * @generated
   */
  public Teste(){
  }


  /**
   * Obtém id
   * return id
   * @generated
   */
  
  public java.lang.String getId(){
    return this.id;
  }

  /**
   * Define id
   * @param id id
   * @generated
   */
  public Teste setId(java.lang.String id){
    this.id = id;
    return this;
  }

  /**
   * Obtém fdate
   * return fdate
   * @generated
   */
  
  public java.util.Date getFdate(){
    return this.fdate;
  }

  /**
   * Define fdate
   * @param fdate fdate
   * @generated
   */
  public Teste setFdate(java.util.Date fdate){
    this.fdate = fdate;
    return this;
  }

  /**
   * Obtém ftimeStamp
   * return ftimeStamp
   * @generated
   */
  
  public java.util.Date getFtimeStamp(){
    return this.ftimeStamp;
  }

  /**
   * Define ftimeStamp
   * @param ftimeStamp ftimeStamp
   * @generated
   */
  public Teste setFtimeStamp(java.util.Date ftimeStamp){
    this.ftimeStamp = ftimeStamp;
    return this;
  }

  /**
   * Obtém finteger
   * return finteger
   * @generated
   */
  
  public java.lang.Integer getFinteger(){
    return this.finteger;
  }

  /**
   * Define finteger
   * @param finteger finteger
   * @generated
   */
  public Teste setFinteger(java.lang.Integer finteger){
    this.finteger = finteger;
    return this;
  }

  /**
   * Obtém flong
   * return flong
   * @generated
   */
  
  public java.lang.Long getFlong(){
    return this.flong;
  }

  /**
   * Define flong
   * @param flong flong
   * @generated
   */
  public Teste setFlong(java.lang.Long flong){
    this.flong = flong;
    return this;
  }

  /**
   * Obtém fdouble
   * return fdouble
   * @generated
   */
  
  public java.lang.Double getFdouble(){
    return this.fdouble;
  }

  /**
   * Define fdouble
   * @param fdouble fdouble
   * @generated
   */
  public Teste setFdouble(java.lang.Double fdouble){
    this.fdouble = fdouble;
    return this;
  }

  /**
   * Obtém fboolean
   * return fboolean
   * @generated
   */
  
  public java.lang.Boolean getFboolean(){
    return this.fboolean;
  }

  /**
   * Define fboolean
   * @param fboolean fboolean
   * @generated
   */
  public Teste setFboolean(java.lang.Boolean fboolean){
    this.fboolean = fboolean;
    return this;
  }

  /**
   * Obtém ffloat
   * return ffloat
   * @generated
   */
  
  public float getFfloat(){
    return this.ffloat;
  }

  /**
   * Define ffloat
   * @param ffloat ffloat
   * @generated
   */
  public Teste setFfloat(float ffloat){
    this.ffloat = ffloat;
    return this;
  }

  /**
   * @generated
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Teste object = (Teste)obj;
    if (id != null ? !id.equals(object.id) : object.id != null) return false;
    return true;
  }

  /**
   * @generated
   */
  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

}
