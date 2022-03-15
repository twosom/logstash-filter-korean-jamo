# Logstash Korean Jamo Filter [![Build Status](https://app.travis-ci.com/twosom/logstash-filter_korean_jamo.svg?branch=master)](https://app.travis-ci.com/twosom/logstash-filter_korean_jamo)

이 플러그인은 [Logstash](https://github.com/elastic/logstash) 를 위한 필터 플러그인입니다.

## Documentation

이 플러그인은 한국어 인덱싱을 위한 Java기반의 logstash 의 한글 자모분리 필터입니다.

자모분리 및 초성추출, 한영전환을 위한 구성을 손쉽게 할 수 있습니다.

## 사용방법

### 1. 설치

이 플러그인을 사용하기 위해서는 맨 처음에 `logstash-core` 라이브러리가 필요합니다.

1

```shell
git clone https://github.com/elastic/logstash.git
```

2

``` shell
cd ./logstash
```

3

``` shell
./gradlew clean assemble
```

4

``` shell
export LOGSTASH_CORE_PATH=$PWD/logstash-core
```

5

``` shell
cd ../
```

6

``` shell
git clone https://github.com/twosom/logstash-filter-korean-jamo.git
```

7

``` shell
echo "LOGSTASH_CORE_PATH=$LOGSTASH_CORE_PATH" >> gradle.properties
```

8

``` shell
./gradlew clean gem
```

9

``` shell
export KOREAN_JAMO_PATH=$PWD/logstash-filter-korean_jamo-현재 자모필터 플러그인 버전.gem  
```

로그스태시가 설치 된 폴더로 이동 후

``` shell
/bin/logstash-plugin install $KOREAN_JAMO_PATH 
```

### 2. 필터 설정

Add the following inside the filter section of your logstash configuration:

```sh

filter {
  korean_jamo {
    chosung => {                          # 초성 추출 설정입니다.
      field => [                          # field 안에 초성 추출 하고자 하는 필드들을 "배열"로 작성합니다.
        "field1",                         # [field1][chosung] 안에 추출 된 초성이 저장됩니다. 
        "field2"                          # [field2][chosung] 안에 추출 된 초성이 저장됩니다.
      ]
    }
    
    jamo => {                             # 자모 분리 설정입니다.
      field => [                          # field 안에 자모 분리 하고자 하는 필드들을 "배열"로 작성합니다.
        "field3",                         # [field3][jamo] 안에 분리 된 자모가 저장됩니다.
        "field4"                          # [field4][jamo] 안에 분리 된 자모가 저장됩니다.
      ]
    }
    
    kortoeng => {                         # 한영 전환 설정입니다. 예를 들어 "깃허브"라는 단어가 있으면 rltgjqm로 전환해줍니다.
      field => [                          # field 안에 한영 전환 하고자 하는 필드들을 "배열"로 작성합니다.
        "field5",                         # [field5][kortoeng] 안에 한영 전환 된 값이 저장됩니다.
        "field6"                          # [field6][kortoeng] 안에 한영 전환 된 값이 저장됩니다.
      ]
    } 
                                          # 모든 설정들은 [필드명][original] 원본 값을 저장합니다.
  }
}
```


### 3. 예제

``` bash
/bin/logstash -e "input { generator {'message' => '안녕하세요.'} } filter { korean_jamo { jamo => { field => [ 'message' ] } } }  output { stdout{} }"
```
