package com.example.boardservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.catalina.User;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

// automatically generate getter/tostring methods for class variables (Lombok)
@Getter
@ToString(callSuper = true)
// index these columns (JPA)
@Table(indexes = {
    @Index(columnList = "title"),
    // note how inherited columns can directly be designated for indexing here
    @Index(columnList = "createdAt"),
    @Index(columnList = "createdBy")
})
@Entity
public class Article extends AuditingFields {
  // declare primary key and how to generate said key (JPA)
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter @ManyToOne(optional = false) @JoinColumn(name = "userId") private UserAccount userAccount;

  // note that id does not have annotation @Setter
  // we want id to be unchangeable
  // @Column(...) to designate columns as not null
  // default is nullable, so if no annotation nullable is implied
  @Setter @Column(nullable = false) private String title;
  @Setter @Column(nullable = false, length = 10000) private String content;

  @ToString.Exclude
  // in article-hashtag relationship, we want to make article own the relationship
  // so use annotation @JoinTable
  // what does it mean to 'own' a relationship?
  // read: https://www.javacodegeeks.com/2013/04/jpa-determining-the-owning-side-of-a-relationship.html
  // or https://stackoverflow.com/questions/2749689/what-is-the-owning-side-in-an-orm-mapping
  // or https://stackoverflow.com/questions/21985308/how-is-the-owning-side-of-this-many-to-many-relationship-determined
  // tl;dr: relates to the side of the relation that 'owns' the foreign key in database
  @JoinTable(
      // specify table and column names instead of having jakarta auto-generate them
      name = "article_hashtag",
      joinColumns = @JoinColumn(name = "articleId"),
      inverseJoinColumns = @JoinColumn(name = "hashtagId")
  )
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private Set<Hashtag> hashtags = new LinkedHashSet<>();

  // don't generate tostring method for this field to avoid circular referencing between articlecomment
  @ToString.Exclude
  @OrderBy("createdAt DESC")
  @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
  // @onetomany using generics (articlecomment), automatically links entities article and articlecomment
  private final Set<ArticleComment> articleComments = new LinkedHashSet<>();


  // a no-param protected or public constructor is required by framework
  protected Article() {}

  private Article(UserAccount userAccount, String title, String content) {
    this.userAccount = userAccount;
    this.title = title;
    this.content = content;
  }

  // factory constructor
  // of is shorthand for method name
  // https://stackoverflow.com/questions/48256270/what-does-the-naming-convention-of-mean-in-java
  public static Article of(UserAccount userAccount, String title, String content) {
    return new Article(userAccount, title, content);
  }

  public void addHashtag(Hashtag hashtag) {
    this.getHashtags().add(hashtag);
  }

  public void addHashtags(Collection<Hashtag> hashtags) {
    this.getHashtags().addAll(hashtags);
  }

  public void clearHashtags() {
    this.getHashtags().clear();
  }

  // if article has not been given an id (null) by the database
  // consider as non-equal even if the other article has identical variable values
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Article article)) return false;
    return this.getId() != null && this.getId().equals(article.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getId());
  }
}
