package com.lwouis.falcon9.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

import javafx.scene.image.Image;

@Entity(name = "Item")
public class Item {

  @Id
  @GeneratedValue
  private Integer id;

  @Column(name = "name")
  private String name;

  @Column(name = "absolutePath")
  private String absolutePath;

  @Column(name = "image")
  @Type(type = "com.lwouis.falcon9.persistence.JavafxImageUserType")
  private Image image;

  public Item() {
  }

  public Item(String name, String absolutePath, Image image) {
    this.name = name;
    this.absolutePath = absolutePath;
    this.image = image;
  }

  public Image getImage() {
    return image;
  }

  public String getName() {
    return name;
  }

  public String getAbsolutePath() {
    return absolutePath;
  }
}
