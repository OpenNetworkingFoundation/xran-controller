COMPILE_DEPS = [
    '//lib:CORE_DEPS',
    '//lib:COMPILE',
    '//lib:netty-handler',
    '//lib:javax.ws.rs-api',
    '//lib:org.apache.karaf.shell.console',
    '//lib:netty-buffer',
    '//utils/rest:onlab-rest',
    '//core/store/serializers:onos-core-serializers',
    '//cli:onos-cli',
    '//lib:netty-transport',
    '//core/common:onos-core-common',
    ':netty-transport-sctp',
]

BUNDLES = [
    '//apps/xran:onos-apps-xran',
]

EXCLUDED_BUNDLES = [
    ':netty-transport-sctp',
]

osgi_jar (
    deps = COMPILE_DEPS,
    web_context = '/onos/xran',
    api_title = 'XRAN REST API',
    api_package = 'org.onosproject.xran.rest',
    api_version = '1.0',
    api_description = 'XRAN REST API',
)

onos_app (
    app_name = 'org.onosproject.xran',
    title = 'XRAN REST API',
    category = 'Utilities',
    url = 'http://onosproject.org',
    description = 'XRAN REST API.',
    included_bundles = BUNDLES,
    excluded_bundles = EXCLUDED_BUNDLES,
)

remote_jar (
    name = 'netty-transport-sctp',
    out = 'netty-transport-sctp-4.1.13.Final.jar',
    url = 'mvn:io.netty:netty-transport-sctp:jar:4.1.13.Final',
    sha1 = '41e4ab1dc14cae445f93cef6421ce08f82804c1d',
    maven_coords = 'io.netty:netty-transport-sctp:4.1.13.Final',
    visibility = [ 'PUBLIC' ],
)
