import spock.lang.Specification
import groovyx.net.http.HttpResponseException

import org.jfrog.artifactory.client.model.repository.settings.impl.MavenRepositorySettingsImpl

import static org.jfrog.artifactory.client.ArtifactoryClient.create

class LayoutEnforcementTest extends Specification {
    def 'layout enforcement test'() {
        setup:
        def baseurl = 'http://localhost:8088/artifactory'
        def artifactory = create(baseurl, 'admin', 'password')

        def builder = artifactory.repositories().builders()
        def local = builder.localRepositoryBuilder().key('maven-local')
        .repositorySettings(new MavenRepositorySettingsImpl()).build()
        artifactory.repositories().create(0, local)

        def jarfile = new File('./src/test/groovy/LayoutEnforcementTest/guava-18.0.jar')

        when:
        artifactory.repository('maven-local').upload('com/google/guava/guava/18.0/guava-18.0.jar', jarfile).doUpload()
    
        then:
        notthrown(HttpResponseException)
        
        when:
        artifactory.repository('maven-local').upload('some/path/file', new ByteArrayInputStream('test'.getBytes('utf-8'))).doUpload()
        
        then:
        thrown(HttpResponseException)

        cleanup:
        artifactory.repository('maven-local').delete()
    }
}
