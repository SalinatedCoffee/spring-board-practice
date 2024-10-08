package com.example.boardservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@ToString(callSuper = true)
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
  @Setter
  @ManyToOne(optional = false)
  private Article article;

  @Setter
  @JoinColumn(name = "userId")
  @ManyToOne(optional = false)
  private UserAccount userAccount;

  @Setter
  @Column(updatable = false)
  private Long parentCommentId;

  @ToString.Exclude
  @OrderBy("createdAt ASC")
  @OneToMany(mappedBy = "parentCommentId", cascade = CascadeType.ALL)
  private Set<ArticleComment> childComments = new LinkedHashSet<>();

  @Setter
  @Column(nullable = false, length = 500)
  private String content;

  protected ArticleComment() {}

  private ArticleComment(Article article, UserAccount userAccount, Long parentCommentId, String content) {
    this.article = article;
    this.userAccount = userAccount;
    this.parentCommentId = parentCommentId;
    this.content = content;
  }

  public static ArticleComment of(Article article, UserAccount userAccount, String content) {
    return new ArticleComment(article, userAccount, null, content);
  }

  public void addChildComment(ArticleComment child) {
    child.setParentCommentId(this.getId());
    this.getChildComments().add(child);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArticleComment that)) return false;
    return this.getId() != null && this.getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getId());
  }
}
