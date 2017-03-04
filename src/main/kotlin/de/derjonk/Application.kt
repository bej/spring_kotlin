package de.derjonk

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.webmvc.BasePathAwareController
import org.springframework.data.rest.webmvc.RepositoryRestController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import javax.persistence.*


@Entity data class Person (var name: String = "") {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long = 0
}

@Entity data class TestCase (var name: String = "") {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long = 0
    @ManyToOne var testSuite: TestSuite? = null
    @Embedded var testRun: TestRun = TestRun()
}

@Entity data class TestSuite (var name: String = "") {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long = 0
}

@Embeddable
data class TestRun (var success: Boolean = false)

@RepositoryRestResource interface PersonRepository: PagingAndSortingRepository<Person, Long>

@RepositoryRestResource interface TestCaseRepository: PagingAndSortingRepository<TestCase, Long>

@RepositoryRestResource interface TestSuiteRepository: PagingAndSortingRepository<TestSuite, Long>

@RepositoryRestController
@BasePathAwareController
@RequestMapping("/testCases/{id}/run")
open class TestRunController @Autowired constructor (val repository:TestCaseRepository) {

    @PostMapping
    fun runTest(testCase: TestCase): ResponseEntity<TestRun> {
        testCase.testRun = TestRun(true);

        this.repository.save(testCase)

        return ResponseEntity<TestRun>(testCase.testRun, HttpStatus.CREATED)
    }
}

@SpringBootApplication
@EnableTransactionManagement
open class Application {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}

