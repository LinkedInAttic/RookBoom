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

var LoginView = Backbone.View.extend({

    initialize: function () {
        _.bindAll(this, 'showDialog', 'onLogin', 'blackoutDialog');
    },

    showDialog: function (element, userOptions) {
        var defaults = {
            message: '',
            success: function (p) {
            }
        };
        var options = $.extend({}, defaults, userOptions);
        var onLogin = _.bind(this.onLogin, this, options.success);
        var view = this;

        var isHeader = $(element).parents('#board-header').size() > 0;

        this.options.dialog.showPopover(
            'login_dialog',
            {
                text: options.message
            },
            element,
            {
                onShow: function () {
                    $('#login-form').ajaxForm($.withErrorHandling({
                        beforeSubmit: view.blackoutDialog,
                        success: onLogin,
                        error: function() {
                            this.options.dialog.hide();
                        },
                        errorMsg: 'Unable to login.</br>Use Cinco credentials to login.'
                    }));
                    $('#j_username').focus();
                },
                container: isHeader ? '#board-header' : 'body'
            }
        );
    },

    blackoutDialog: function () {
        this.options.dialog.blackout();
    },

    onLogin: function (success, data) {
        this.options.dialog.hide();
        if (!data.principal || !data.principal.loggedIn) {
            $.errorMsg('Incorrect username/password.</br>Use Cinco credentials to login.');
        } else {
            this.model.set('principal', data.principal);
            this.options.attendees.addAttendee(data.principal.user.address);
            success(data.principal);
            $.gaq('login', 'logged-in', data.principal.user.address);
        }
    }
});