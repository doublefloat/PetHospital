package base.controller;

import base.dao.ArticleDao;
import base.model.Article;
import base.model.Page;
import base.model.response.ResponseWrapper;
import base.service.ArticleService;
import base.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author long
 * @since 18-4-4
 */
@RestController
public class ArticleController extends BaseController {

  private ArticleDao articleDao;

  private ArticleService articleService;

  @Autowired
  public void setArticleDao(ArticleDao articleDao) {
    this.articleDao = articleDao;
  }

  @Autowired
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  @GetMapping(value = "/article")
  public ResponseWrapper retrieve(@RequestParam Integer page,
      @RequestParam Integer pageSize) {
    return responsePacker.pack(JsonUtil.toMap("page", page, "pageSize", pageSize), () ->
        articleDao.retrieve(new Page(page, pageSize)));
  }

  @GetMapping("/article/{createdTime}/{rand}")
  public ResponseWrapper retrieveArticle(@PathVariable Long createdTime,
      @PathVariable Integer rand) {
    return responsePacker.pack(null,
        () -> articleService.retrieve(new Article(createdTime, rand, null)));
  }

  @GetMapping(value="/article/count")
  public ResponseWrapper count() {
    return responsePacker.pack(null, () -> {
      List<Long> result = new ArrayList<>(1);
      result.add(articleDao.count());
      return result;
    });
  }

  @DeleteMapping(value = "/article")
  public ResponseWrapper delete(@RequestParam Long timestamp,
      @RequestParam Integer rand) {
    return messagePacker.pack(JsonUtil.toMap("timestamp", timestamp, "rand", rand), () -> {
      articleService.delete(new Article(timestamp, rand, null));
      return null;
    });
  }

  @PostMapping(value = "/article")
  public ResponseWrapper create(@RequestParam(required = false) String description) {
    return responsePacker.pack(JsonUtil.toMap("description", description), () -> {
      Article article = new Article(description);
      if (!thriceCreate(article)) {
        throw new RuntimeException("Creation is failed");
      }
      List<Article> result = new ArrayList<>(1);
      result.add(article);
      return result;
    });
  }

  private boolean thriceCreate(Article article) {
    for (int i = 0; i < 3; i++) {
      try {
        articleDao.create(article);
        return true;
      } catch (Exception e) {
        article.flip();
      }
    }
    return false;
  }

  @PutMapping(value = "/article")
  public ResponseWrapper update(@RequestBody Article article) {
    return messagePacker.pack(JsonUtil.toMap(article), () -> {
      if (0 == articleDao.update(article)) {
        throw new RuntimeException("Update is failed");
      }
      return null;
    });
  }
}