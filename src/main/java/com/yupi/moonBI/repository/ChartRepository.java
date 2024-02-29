package com.yupi.moonBI.repository;


import com.yupi.moonBI.model.document.MongoChart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
public interface ChartRepository extends MongoRepository<MongoChart, String> {

    @Query("{'userId': ?0}")
    Page<MongoChart> findAllByUserId(long userId, Pageable pageable);

    @Query("{'chartId': ?0}")
    List<MongoChart> findAllByChartId(long chartId);

    long deleteAllByChartId(long chartId);

    MongoChart findByChartId(long chartId);

}
