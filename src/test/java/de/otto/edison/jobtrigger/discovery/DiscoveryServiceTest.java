package de.otto.edison.jobtrigger.discovery;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import de.otto.edison.jobtrigger.definition.JobDefinition;
import de.otto.edison.registry.api.Link;
import de.otto.edison.registry.service.RegisteredService;
import de.otto.edison.registry.service.Registry;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.registry.api.Link.link;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DiscoveryServiceTest {

    public static final String ENV_NAME = "someEnv";

    @InjectMocks
    DiscoveryService discoveryService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AsyncHttpClient httpClient;

    @Mock
    private Registry serviceRegistry;

    @Before
    public void setUp() throws Exception {
        reset(httpClient, serviceRegistry);
    }

    @Test
    public void shouldConvertToJobDefinitions() throws IOException {
        RegisteredService service = someService();
        JobDefinitionRepresentation jobDefinitionRepresentation = new JobDefinitionRepresentation();
        jobDefinitionRepresentation.setCron("* * * * * *");
        jobDefinitionRepresentation.setFixedDelay(12l);
        jobDefinitionRepresentation.setLinks(ImmutableList.of(link("http://github.com/otto-de/edison/link-relations/job/trigger", "href",  "title")));
        jobDefinitionRepresentation.setName("MyJob");
        jobDefinitionRepresentation.setRetries(12);
        jobDefinitionRepresentation.setRetryDelay(12l);
        jobDefinitionRepresentation.setType("someType");


        Response response = mock(Response.class);

        when(response.getResponseBody()).thenReturn(
                new Gson().toJson(jobDefinitionRepresentation)
        );
        JobDefinition jd = discoveryService.jobDefinitionFrom("someDefinitionUrl", service, response);

        assertThat(discoveryService, is(not(nullValue())));
        assertThat(jd.getCron(), is(Optional.of("* * * * * *")));
        assertThat(jd.getDefinitionUrl(), is("someDefinitionUrl"));
        assertThat(jd.getDescription(), is("MyJob"));
        assertThat(jd.getEnv(), is(ENV_NAME));
        assertThat(jd.getFixedDelay(), is(Optional.of(Duration.ofSeconds(12l))));
        assertThat(jd.getJobType(), is("someType"));
        assertThat(jd.getRetries(), is(12));
        assertThat(jd.getRetryDelay(), is(Optional.of(Duration.ofSeconds(12l))));
        assertThat(jd.getService(), is("myService"));
        assertThat(jd.getTriggerUrl(), is("href"));
    }

    private RegisteredService someService() {
        return new RegisteredService(
                "myService",
                "someHref",
                "someDescription",
                Duration.ZERO,
                "someEnv",
                ImmutableList.of());
    }

    @Ignore("Ignored until Validation is implemented")
    @Test
    public void shouldValidateResponseBody() throws IOException {
        RegisteredService service = someService();

        Response response = mock(Response.class);

        when(response.getResponseBody()).thenReturn(
                ""
        );
        JobDefinition jobDefintion = discoveryService.jobDefinitionFrom("someDefinitionUrl", service, response);

        assertThat(jobDefintion, is(not(nullValue())));
    }

    @Test
    public void shouldFetchJobDefinitionsURLsForEveryService() throws Exception {
        when(serviceRegistry.findServices())
                .thenReturn(ImmutableList.of(someService(), someService()));
        when(httpClient.prepareGet(any()).execute().get()).thenReturn(mock(Response.class));

        discoveryService.rediscover();

        verify(httpClient, times(2)).prepareGet("someHref/internal/jobdefinitions");
    }

    @Test
    @Ignore("deep stubbing doesn't work with powermock, finding another solution")
    public void shouldFetchJobDefinitions() throws Exception {
        LinksRepresentation linksRepresentation = new LinksRepresentation();
        linksRepresentation.setLinks(ImmutableList.of(
                link(DiscoveryService.JOB_DEFINITION_LINK_RELATION_TYPE, "someHref" ,"someTitle"),
                link(DiscoveryService.JOB_DEFINITION_LINK_RELATION_TYPE, "someOtherHref" ,"someOtherTitle")));

        Response response = mock(Response.class);
        when(response.getResponseBody()).thenReturn(
                new Gson().toJson(linksRepresentation)
        );


        when(httpClient.prepareGet(any()).execute().get()).thenReturn(mock(Response.class));

        List<JobDefinition> jobDefinitions = discoveryService.jobDefinitionsFrom(someService(), response);

        verify(httpClient, times(2)).prepareGet("someHref/internal/jobdefinitions");
    }
}