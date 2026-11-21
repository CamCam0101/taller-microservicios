<?php

use App\Presentation\Repositories\AccionesRepository;
use Slim\App;
use Slim\Routing\RouteCollectorProxy;

return function (App $app) {
    $app->group('/acciones', function (RouteCollectorProxy $group) {
        $group->get('', [AccionesRepository::class, 'get_all_acciones']);
        $group->get('/sprint/{sprint_id}', [AccionesRepository::class, 'get_acciones_by_sprint']);
        $group->put('/evaluar-lote', [AccionesRepository::class, 'evaluar_lote']);
        $group->put('/{id}/cumplida', [AccionesRepository::class, 'update_cumplimiento']);
    });
};