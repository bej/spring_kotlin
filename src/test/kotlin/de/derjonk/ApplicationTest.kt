package de.derjonk

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup
import org.springframework.web.context.WebApplicationContext

@RunWith(SpringRunner::class)
@SpringBootTest
class ApplicationTest {

    @Rule @JvmField val restDoc = JUnitRestDocumentation("target/generated-snippets")
    @Autowired lateinit var context: WebApplicationContext
    @Autowired lateinit var testCaseRepository: TestCaseRepository
    @Autowired lateinit var testSuiteRepository: TestSuiteRepository
    @Autowired lateinit var mapper: ObjectMapper
    lateinit var mockMvc: MockMvc
    lateinit var document: RestDocumentationResultHandler

    @Before fun setUp() {
        testCaseRepository.deleteAll()
        testSuiteRepository.deleteAll()
        document = document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        mockMvc = webAppContextSetup(this.context)
                .apply<DefaultMockMvcBuilder>(documentationConfiguration(restDoc))
                .alwaysDo<DefaultMockMvcBuilder>(document).build()
    }

    @Test fun listTestCases() {
        val testSuite = testSuiteRepository.save(TestSuite("Suite 1"))
        val testCase1 = TestCase("Test 1")
        testCase1.testSuite = testSuite
        testCaseRepository.save(testCase1)

        mockMvc.perform(get("/testCases").accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)
                .andDo(document.document(
                            responseFields(
                                fieldWithPath("_embedded.testCases").description("An array of <<resources-testcase, TestCase resources>>"),
                                fieldWithPath("_links").description("<<resources-tags-list-links, Links>> to other resources"),
                                fieldWithPath("page").description("pagination")
                            )
                    )
                )
    }

}
