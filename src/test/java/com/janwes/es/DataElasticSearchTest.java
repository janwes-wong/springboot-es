package com.janwes.es;

import com.janwes.es.pojo.Poetry;
import com.janwes.es.repository.PoetryRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
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
 * @date 2021/8/2 15:53
 * @description
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DataElasticSearchTest {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private PoetryRepository poetryRepository;

    /**
     * 高版本elasticsearch无需手动创建索引和映射
     * 创建索引
     * 创建映射
     */
    @Test
    public void putMapping() {
        // 创建索引
        elasticsearchRestTemplate.createIndex(Poetry.class);
        // 创建映射
        elasticsearchRestTemplate.putMapping(Poetry.class);
    }

    /**
     * 创建文档/更新文档
     * id相同保存时数据将被更新修改
     */
    @Test
    public void createdDocument() {
        poetryRepository.save(new Poetry(103L, "青玉案·元夕", "蓦然回首，那人却在灯火阑珊处"));
    }

    /**
     * 批量创建文档/更新文档
     */
    @Test
    public void batchCreatedDocument() {
        List<Poetry> poetryList = new ArrayList<>();
        for (long i = 6; i <= 100; i++) {
            poetryList.add(new Poetry(i, "滕王阁序 " + i, "落霞与孤鹜齐飞，秋水共长天一色 " + i));
        }
        poetryRepository.saveAll(poetryList);
    }

    /**
     * 根据指定id删除文档
     */
    @Test
    public void delete() {
        poetryRepository.deleteById(10L);
    }

    /**
     * 删除全部文档
     */
    @Test
    public void deleteAll() {
        poetryRepository.deleteAll();
    }

    /**
     * 根据id查询文档
     */
    @Test
    public void findById() {
        Poetry poetry = poetryRepository.findById(20L).get();
        System.out.println(poetry);
    }

    /**
     * 分页查询（不带分页条件）
     */
    @Test
    public void selectPage() {
        /**
         * 构建分页查询条件
         * 参数1 page 表示当前页码
         * 参数2 size 表示每页显示的行，即每页大小
         * 参数3 排序条件
         */
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")); // 根据id升序排序
        // 分页查询
        Page<Poetry> poetryPage = poetryRepository.findAll(pageable);
        // 总记录数
        long totalElements = poetryPage.getTotalElements();
        // 总页数
        int totalPages = poetryPage.getTotalPages();
        // 获取当前页的列表数据
        List<Poetry> poetryList = poetryPage.getContent();
        if (!CollectionUtils.isEmpty(poetryList)) {
            for (Poetry poetry : poetryList) {
                System.out.println("每页数据:" + poetry);
            }
        }
        System.out.println("总记录数:" + totalElements);
        System.out.println("总页数:" + totalPages);
    }

    /**
     * 模糊查询，根据id排序分页查询
     */
    @Test
    public void find() {
        // 关键词
        String keyword = "滕";
        // 当前页码
        int page = 1;
        // 每页显示大小
        int pageSize = 10;

        // 1. 创建查询对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // 2. 设置查询条件
        // 2.1 设置匹配查询
        //nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("title", keyword));
        //nativeSearchQueryBuilder.withQuery(QueryBuilders.fuzzyQuery("title", keyword)); // 模糊查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.termQuery("title", keyword)); // 词条查询
        // 2.2 设置分页条件
        Pageable pageable = PageRequest.of((page - 1), pageSize, Sort.by(Sort.Direction.ASC, "id"));
        nativeSearchQueryBuilder.withPageable(pageable);
        // 2.3 设置高亮
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("title"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder()
                .preTags("<span style=\"color:red\">") // 前缀
                .postTags("</span>")); // 后缀
        // 这样也可以  前缀："<em style=\"color:red\">" 后缀："</em>"
        // 3. 构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        // 4. 根据条件执行查询
        SearchHits<Poetry> searchHits = elasticsearchRestTemplate.search(query, // 查询条件
                Poetry.class, // 设置查询到的结果类型的字节码对象
                IndexCoordinates.of("poetry_index_001"));// 指定查询的索引
        // 5. 获取查询到的总记录数(总命中数)
        long totalHits = searchHits.getTotalHits();
        // 6. 最终获取到的数据封装返回
        List<Poetry> poetryList = new ArrayList<>();
        for (SearchHit<Poetry> searchHit : searchHits) {
            // 5.1 获取文档对应的pojo对象
            Poetry poetry = searchHit.getContent();
            // 5.2 获取高亮字段为title的高亮数据
            List<String> highlightField = searchHit.getHighlightField("title");
            StringBuffer sb = new StringBuffer();
            if (!CollectionUtils.isEmpty(highlightField)) {
                for (String title : highlightField) {
                    sb.append(title);
                }
            }
            // 5.3将高亮数据重新设置回去
            poetry.setTitle(sb.toString());
            poetryList.add(poetry);
        }
        // 7. 设置计算总页数
        long totalPages = totalHits % pageSize == 0 ? totalHits / pageSize : totalHits / pageSize + 1;

        System.out.println("总记录数:" + totalHits);
        System.out.println("总页数:" + totalPages);
        System.out.println("每页数据:" + poetryList.toString());
    }
}
