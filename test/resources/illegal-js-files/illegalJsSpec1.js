'use strict';

ddescribe('test', function () {
    it('should pass', inject(function () {
        var list = ["a", "b", "c"];
        expect(list.length).toEqual(3);
    }));
});