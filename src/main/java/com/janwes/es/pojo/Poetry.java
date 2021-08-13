package com.janwes.es.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * @author Janwes
 * @version 1.0
 * @package com.janwes.es.pojo
 * @date 2021/8/2 15:28
 * @description elasticsearch搜索引擎实体类
 * 加上了@Document注解之后，默认情况下这个实体中所有的属性都会被建立索引、并且分词
 * 在Elasticsearch 6.X 版本中，不建议使用type，而且在7.X版本中将会彻底废弃type(type已过期，默认就是_doc)
 * 注解@Id 修饰字段 标识 该字段就是文档的唯一标识
 * 注解@Field(type = FieldType.Text,index = true,store = false,searchAnalyzer = "ik_smart",analyzer = "ik_smart")
 * ik_smart：会将文本做最粗粒度的拆分
 * ik_max_word：会将文本做最细粒度的拆分
 * <p>
 * 注解@field 用于建立 字段的映射关系
 * type 指定字段的数据类型  text 标识文本 要分词的
 * index 是否索引 默认是要索引
 * store 是否存储 默认是不存储  默认是在_source中存储了数据  这个存储是指 是否存储到Lucene中
 * analyzer: 指定分词器 建立倒排索引的时候使用的分词器
 * searchAnalyzer 指定搜索的时候对搜索的文本的分词器  默认采用相同的分词器即可 这个可以不用配置
 * 不配置field 将会采用默认的的配置(映射)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "poetry_index_001")
public class Poetry implements Serializable {

    private static final long serialVersionUID = -4286726096417755534L;

    /**
     * 表示该字段的值存放到索引库的_id字段上，表示主键
     */
    @Id
    private Long id;

    /**
     * 文章标题
     * index默认为true
     */
    @Field(index = true, type = FieldType.Text, analyzer = "ik_smart", store = false, searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 文章内容
     */
    @Field(index = true, type = FieldType.Text, analyzer = "ik_smart", store = false, searchAnalyzer = "ik_smart")
    private String content;
}
