var DustTools = {

    compile: function(rawSource, name) {
        return dust.compile(rawSource, name)
    },

    compileAndLoad: function(rawSource, name) {
        dust.loadSource(dust.compile(rawSource, name))
    },

    load: function(compiled) {
        dust.loadSource(compiled)
    },

    render: function(name, json, writer) {
        var base = dust.makeBase({})

        dust.render(
            name,
            base.push(JSON.parse(json)),
            function(err, data) {
                if(err) {
                    writer.write(err)
                } else {
                    writer.write( data )
                }
            }
        )
        return ''
    }
};

dust.filters.json = function(value) {
    return JSON.stringify(value);
};
