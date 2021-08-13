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
 * @date 2021/8/6 15:09
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "product_index_001")
public class Product implements Serializable {

    private static final long serialVersionUID = -720798382169794132L;

    @Id
    private Integer id;

    /**
     * 商品名称
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
    private String name;

    private double originalPrice;

    private double promotePrice;

    private String pushDate;
}
