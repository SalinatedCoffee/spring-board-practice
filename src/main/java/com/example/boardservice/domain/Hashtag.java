package com.example.boardservice.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
// set callSuper to True to include AuditingFields when toString is called on Hashtag object
@ToString(callSuper = true)
@Table(indexes = {
    @Index(columnList = "hashtagName", unique = true),
    @Index(columnList = "createdAt"),
    @Index(columnList = "createdBy")
})
@Entity
public class Hashtag extends AuditingFields {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @ToString.Exclude @ManyToMany(mappedBy = "hashtags") private Set<Article> articles = new LinkedHashSet<>();
  @Setter @Column(nullable = false) private String hashtagName;

  // opinions/best practices regarding empty constructors
  // read: https://stackoverflow.com/questions/18993936/how-to-best-explain-and-use-empty-constructors-in-java
  protected Hashtag() {}

  private Hashtag(String hashtagName) {
    this.hashtagName = hashtagName;
  }

  public static Hashtag of(String hashtagName) {
    return new Hashtag(hashtagName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    // check if o is of type Hashtag, and if it is cast it into one
    // read https://openjdk.org/jeps/394
    if (!(o instanceof Hashtag that)) return false;
    // because of the previous line we can refer to the object that in this line
    // this keyword is not required, but added here for readability
    return this.getId() != null && this.getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getId());
  }
}
