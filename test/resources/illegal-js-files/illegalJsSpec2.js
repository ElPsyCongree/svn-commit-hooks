'use strict';

describe('test', function () {
    iit('should pass', inject(function () {
        var list = ["a", "b", "c"];
        expect(list.length).toEqual(3);
    }));
});