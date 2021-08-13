package com.janwes.es.repository;

import com.janwes.es.pojo.Poetry;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Janwes
 * @version 1.0
 * @package com.janwes.es.repository
 * @date 2021/8/2 15:45
 * @description
 * 继承ElasticsearchRepository接口，该接口泛型参数说明如下：
 * 第一个参数：指映射文档document
 * 第二个参数：指映射文档document的主键
 */
public interface PoetryRepository extends ElasticsearchRepository<Poetry, Long> {
}
