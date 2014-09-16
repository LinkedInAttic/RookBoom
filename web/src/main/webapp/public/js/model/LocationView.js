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

var LocationView = Backbone.View.extend({

    events: {
        'click #location-info p': 'selectDialog',
        'click #locations-select .location-option': 'changeLocation'
    },

    initialize: function () {
        _.bindAll(this, 'render', 'selectDialog', 'changeLocation');
        this.options.filterValues.bind('change:location', this.render);
        this.setElement('body');
        this.render();
    },

    render: function () {
        var id = this.options.filterValues.get('location');
        $.dustReplace('location', this.model.getById(id), $('#location-info'));
    },

    selectDialog: function () {
        var locations = _.sortBy(this.model.get('config'), 'name'),
            columns = 3,
            size = (locations.length + columns - 1) / columns,
            locationsInColumns = _.map(_.range(0, locations.length, size), function(idx) {
                return locations.slice(idx, idx + size);
            });

        this.options.dialog.showModal(
            'location_select',
            {
                locations: locationsInColumns
            },
            {
                container: '#location-info'
            }
        );
    },

    changeLocation: function ($event) {
        var $target = $($event.target);
        var siteId = $target.attr('siteId');
        this.model.setById(siteId);
        this.options.dialog.hide();
        $.gaq('location', 'location-switched', siteId);
    }
});