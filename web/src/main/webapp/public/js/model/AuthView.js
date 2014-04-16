/*
 * (c) Copyright 2014 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var AuthView = Backbone.View.extend({

    events: {
        'click #login': function () {
            this.options.loginView.showDialog('#login', {message: 'Use Cinco credentials to login'});
        },
        'click #logout': function () {
            var self = this;
            $.ajax($.withErrorHandling({
                url: '/j_spring_security_logout',
                success: function () {
                    self.options.dialog.hide();
                    var address = self.model.get('principal').user.address;
                    self.model.set('principal', {loggedIn: false, user: {}});
                    $.gaq('login', 'logged-out', address);
                },
                error: function() {
                    self.options.dialog.hide();
                }
            }));
        },
        'click #auth-principal': function () {
            this.options.dialog.showPopover('user_menu', {}, '#auth-principal', {
                container: '#auth-principal'
            });
        }
    },

    initialize: function () {
        _.bindAll(this, 'render');
        this.model.bind('change', this.render);
        this.setElement('#auth-info');
    },

    render: function () {
        $.dustReplace('auth', {principal: this.model.get('principal')}, this.$el);
    }
});