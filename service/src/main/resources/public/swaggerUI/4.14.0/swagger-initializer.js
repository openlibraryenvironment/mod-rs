window.onload = function() {
  //<editor-fold desc="Changeable Configuration Block">

function HideTopbarPlugin() {
  // this plugin overrides the Topbar component to return nothing
  return {
    components: {
      Topbar: function() { return null }
    }
  }
}

function HideauthorizeButtonPlugin() {
  // this plugin overrides the Topbar component to return nothing
  return {
    components: {
      authorizeBtn: function() { return null }
    }
  }
}

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  window.ui = SwaggerUIBundle({
    url: "/rs/swagger/api",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl,
      // Uncomment the following line if you do not want the top bar
      //HideTopbarPlugin,
      HideauthorizeButtonPlugin
    ],
    layout: "StandaloneLayout"
  });

  //</editor-fold>
};
