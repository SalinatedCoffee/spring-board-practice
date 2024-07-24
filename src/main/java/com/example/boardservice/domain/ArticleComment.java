package com.example.boardservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
// index these columns (JPA)
@Table(indexes = {
    @Index(columnList = "content"),
    @Index(columnList = "createdAt"),
    @Index(columnList = "createdBy")
})
@Entity
// inherit class with fields to extend this class to 'link' the two modules
// the superclass needs to be annotated with @MappedSuperclass
public class ArticleComment extends AuditingFields {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  // can also be implemented without annotation, eg. private Long articleId
  // but best practice is to decouple and use annotation
  @Setter @ManyToOne(optional = false) private Article article;
  @Setter @ManyToOne(optional = false) @JoinColumn(name = "userId") private UserAccount userAccount;
  @Setter @Column(nullable = false, length = 500) private String content;

  protected ArticleComment() {}

  private ArticleComment(Article article, UserAccount userAccount, String content) {
    this.article = article;
    this.userAccount = userAccount;
    this.content = content;
  }

  public static ArticleComment of(Article article, UserAccount userAccount, String content) {
    return new ArticleComment(article, userAccount, content);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArticleComment articleComment)) return false;
    return this.getId() != null && this.getId().equals(articleComment.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getId());
  }
}
