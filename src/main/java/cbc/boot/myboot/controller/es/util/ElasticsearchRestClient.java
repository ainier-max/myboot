package cbc.boot.myboot.controller.es.util;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Objects;

/**
 * this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * Create By @author zing  @date 2018/7/18 17:20
 */

@Configuration
public class ElasticsearchRestClient {
    private static final int ADDRESS_LENGTH = 2;
    private static final String HTTP_SCHEME = "http";

    /**
     * 使用冒号隔开ip和端口1
     */
    @Value("${elasticsearch.ip}")
    String[] ipAddress;

    @Bean
    public RestClientBuilder restClientBuilder() {
        HttpHost[] hosts = Arrays.stream(ipAddress)
                .map(this::makeHttpHost)
                .filter(Objects::nonNull)
                .toArray(HttpHost[]::new);
        System.out.println("ElaticSearche服务:"+Arrays.toString(hosts));
        return RestClient.builder(hosts);
    }

    @Bean(name = "highLevelClient")
    public RestHighLevelClient highLevelClient(@Autowired RestClientBuilder restClientBuilder) {
        //restClientBuilder.setMaxRetryTimeoutMillis(60000);
        return new RestHighLevelClient(restClientBuilder);
    }

    private HttpHost makeHttpHost(String s) {
        String[] address = s.split(":");
        if (address.length == ADDRESS_LENGTH) {
            String ip = address[0];
            int port = Integer.parseInt(address[1]);
            return new HttpHost(ip, port, HTTP_SCHEME);
        } else {
            return null;
        }
    }
}
