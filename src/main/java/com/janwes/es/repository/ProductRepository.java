package com.janwes.es.repository;

import com.janwes.es.pojo.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Janwes
 * @version 1.0
 * @package com.janwes.es.repository
 * @date 2021/8/6 15:29
 * @description
 */
public interface ProductRepository extends ElasticsearchRepository<Product, Integer> {
}
