package com.janwes.es;

import com.janwes.es.pojo.Poetry;
import com.janwes.es.pojo.Product;
import com.janwes.es.repository.ProductRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Janwes
 * @version 1.0
 * @package com.janwes.es
 * @date 2021/8/6 15:30
 * @description
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    public void testSave() {
        Product product = productRepository.save(new Product(7, "华为Mate40手机 ", 2999.00, 1999.00, "2018-05-05"));
        System.out.println(product);
    }

    @Test
    public void batchSave() {
        List<Product> productList = new ArrayList<>();
        for (int i = 0; i <= 10000; i++) {
            productList.add(new Product(i, "小米手机 " + i, 2999.00 + i, 1999.00 + i, "2018-05-05"));
        }
        productRepository.saveAll(productList);
    }

    @Test
    public void delete() {
        productRepository.deleteAll();
    }

    /**
     * 模糊查询，根据id排序分页查询
     */
    @Test
    public void find() {
        // 关键词
        String keyword = "华为";
        // 当前页码
        int page = 1;
        // 每页显示大小
        int pageSize = 10;

        // 1. 创建查询对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // 2. 设置查询条件
        // 2.1 设置匹配查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keyword));
        //nativeSearchQueryBuilder.withQuery(QueryBuilders.fuzzyQuery("title", keyword)); // 模糊查询
        // 2.2 设置分页条件
        Pageable pageable = PageRequest.of((page - 1), pageSize, Sort.by(Sort.Direction.ASC, "id"));
        nativeSearchQueryBuilder.withPageable(pageable);
        // 2.3 设置高亮
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("name"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder()
                .preTags("<span style=\"color:red\">") // 前缀
                .postTags("</span>")); // 后缀
        // 这样也可以  前缀："<em style=\"color:red\">" 后缀："</em>"
        // 3. 构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        // 4. 根据条件执行查询
        SearchHits<Product> searchHits = elasticsearchRestTemplate.search(query, // 查询条件
                Product.class, // 设置查询到的结果类型的字节码对象
                IndexCoordinates.of("product_index_001"));// 指定查询的索引
        // 5. 获取查询到的总记录数(总命中数)
        long totalHits = searchHits.getTotalHits();
        // 6. 最终获取到的数据封装返回
        List<Product> poetryList = new ArrayList<>();
        for (SearchHit<Product> searchHit : searchHits) {
            // 5.1 获取文档对应的pojo对象
            Product product = searchHit.getContent();
            // 5.2 获取高亮字段为title的高亮数据
            List<String> highlightField = searchHit.getHighlightField("name");
            StringBuilder sb = new StringBuilder();
            if (!CollectionUtils.isEmpty(highlightField)) {
                for (String title : highlightField) {
                    sb.append(title);
                }
            }
            // 5.3将高亮数据重新设置回去
            product.setName(sb.toString());
            poetryList.add(product);
        }
        // 7. 设置计算总页数
        long totalPages = totalHits % pageSize == 0 ? totalHits / pageSize : totalHits / pageSize + 1;

        System.out.println("总记录数:" + totalHits);
        System.out.println("总页数:" + totalPages);
        System.out.println("每页数据:" + poetryList);
    }
}
