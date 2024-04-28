.PHONY: test build deploy

test:
	@mvn clean test --file pom.xml

build:
	@mvn clean package -D maven.test.skip=false

deploy:
	@mvn clean deploy -DskipTests
