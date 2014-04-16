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

var Location = Backbone.Model.extend({

    COOKIE: 'location',

    defaults: {
        location: {
            name: '',
            id: '',
            timezoneOffset: 0
        }
    },

    initialize: function () {
        _.bindAll(this, 'init', 'onChange', 'setById', 'getById', 'getTimezoneOffsetById', 'getDayForLocation', 'adjustTime', 'getCurrentTimeForLocation', 'getCurrentSiteId');
        this.get('filterValues').bind('change:location', this.onChange);
        this.init();
    },

    init: function () {
        this.setById($.cookie(this.COOKIE));
    },

    comparator: function (value) {
        return function (i) {
            return i.id === value;
        };
    },

    setById: function (siteId) {
        var exists = _.any(this.get('config'), this.comparator(siteId));
        var id = exists ? siteId : this.get('defaultLocationId');
        this.get('filterValues').set('location', id);
    },

    getById: function (siteId) {
        var location = _.find(this.get('config'), this.comparator(siteId));
        return _.isUndefined(location) ? this.get('config')[0] : location;
    },

    getCurrentSiteId: function () {
        return this.get('filterValues').get('location');
    },

    getTimezoneOffsetById: function (siteId) {
        return parseInt(this.getById(siteId).timezoneOffset);
    },

    getDayForLocation: function () {
        var day = this.get('filterValues').get('day');
        return this.adjustTime(day, true);
    },

    getCurrentTimeForLocation: function () {
        return this.adjustTime(new Date().getTime(), false);
    },

    adjustTime: function (time, backward) {
        var siteId = this.get('filterValues').get('location');
        var locationOffset = this.getTimezoneOffsetById(siteId);
        var timezoneCorrection = (locationOffset + new Date().getTimezoneOffset()) * 60 * 1000;
        return time + (backward ? -timezoneCorrection : timezoneCorrection);
    },

    onChange: function () {
        var siteId = this.get('filterValues').get('location');

        $.cookie(this.COOKIE, siteId, {
            expires: new Date('January 1, 2061'),
            path: '/'
        });
    }
});